#!/usr/bin/perl
#
# Simple Daily build script
#
# - make a fresh checkout of pathvisio-trunk in a temp dir
# - compile v1 and v2, check for errors
# - run unit tests
# - if a step goes wrong, send email
#
# Note: this script needs to be run by the user that has
# passwordless ssh access to the target site
#
# If you want to run this from anacron, you'll have to
# make sure root has a key that is on .authorized_hosts on the target
# Pay attention to this, scp fails if you need to enter a password!
#

use warnings;
use strict;

###############
#   globals
###############

use File::Temp qw / tempfile tempdir /;
use File::Find;

# If $fCleanCheckout == 1, svn checkout will be done in a fresh temp directory.
# If $fCleanCheckout == 0, svn checkout will be done in $defaultDir.
my $fCleanCheckout = 0;

# these people will be emailed when a problem occurs
my @emails = (
    'martijn.vaniersel@bigcat.unimaas.nl', 
#    'wikipathways-commit@googlegroups.com',    
#    'thomas.kelder@bigcat.unimaas.nl',
    );

my @steps;

# if fSendEmail is false, the email will not be sent but
# displayed on the screen

my $fSendEmail = 1;

my $basedir = "/home/martijn";
my $cytoscapedir= "$basedir/etc/cytoscape-v2.6.0";
my $fnCheckstyle = "$basedir/cs_pathvisio.txt";
my $defaultDir = "$basedir/temp";

# checkout dir
my $dir;

#################
# initialization
#################

if ($fCleanCheckout)
{
	$dir = tempdir ( CLEANUP => 1 );
}
else
{
	$dir = $defaultDir;
	
	mkdir $dir; # make sure dir exists
	
	# clean the working copy if we can. Ignore failure, we'll do a checkout anyway.
	system ("svn-clean $dir");
}

print "Location: $dir\n";

#################
#  subroutines
#################

# do step is used to break the daily build in logical steps
# each step has a name, an action and possibly a log file.
# do_step dies if the action fails, but not before adding the result to the 
# @steps array
sub do_step
{
	my %args = @_;
	
	eval {
		&{$args{action}}();
	};

	my $error = $@;
	my $result =
		{
			name => $args{name},
			log => $args{log},
			status => ($error ? "FAILURE" : "SUCCESS"),
			error => $error,
		};
	
	push @steps, $result;
		
	#die on error
	if ($error)
	{
		die $error;
	}	
}

##################
#  main
##################

# in the eval below, each step is done consecutively until 
# one step dies, which causes a break out of the eval statement.
eval
{	
	# create a temp dir and do a fresh checkout
	chdir ($dir);

	# First step: do a fresh checkout
	do_step ( 
		name => "SVN CHECKOUT",
		log => "$dir/svnerr.txt",
		action => sub
		{
			# For some reason, our svn server sometimes fails on the first attempt but then works fine afterwards.
			# So if it fails, retry a few times
			my $retries = 5;
			my $fail_delay = 5;
			my $i = 0;
			unlink "$dir/svnerr.txt";
			while (1)
			{
				$i++;

				eval {
					system ("svn checkout http://svn.bigcat.unimaas.nl/pathvisio/trunk . 2>>$dir/svnerr.txt") == 0
						or die "svn checkout failed: $?";
				};
				unless ($@) { last; } # break if success
				if ($i == $retries)
				{
					die ("svn checkout failed after $i attempts\n");
				}
				print "SVN co failed, trying again...\n";
				sleep ($fail_delay);
			}
		}
	);

	# Next step: compile the main project
	do_step (
		name => "COMPILE ALL",
		log => "$dir/compile1.txt",
		action => sub
		{
			# compile
			system ("ant all > $dir/compile1.txt") == 0 or 
				die ("compile all failed with error code ", $? >> 8, "\n");
		}
	);

	# Next step: compile the webservice client library
	do_step (
		name => "COMPILE WEBSERVICE CLIENT LIB",
		log => "$dir/compile-wpc.txt",
		action => sub
		{
			# compile
			system ("ant jar-wpclient " . 
				"-Dwsdl.url=http://137.120.89.38/wikipathways-test/wpi/webservice/webservice.php?wsdl " .
				"> $dir/compile-wpc.txt") == 0 or 
				die ("compile webservice client lib failed with error code ", $? >> 8, "\n");
		}
	);

	# Next step: do all JUnit unit tests
	do_step (
		name => "JUNIT TEST",
		log => "$dir/junit.txt",
		action => sub
		{
			system ("ant test > $dir/junit.txt") == 0 or 
				die ("test failed with error code ", $? >> 8, "\n");
		}
	);

	# Next step: ogretest (test dependencies of core)
	do_step (
		name => "OGRETEST",
		log => "$dir/ogretest.txt",
		action => sub
		{
			# compile
			system ("ant ogretest > $dir/ogretest.txt") == 0 or 
				die ("compile all failed with error code ", $? >> 8, "\n");
		}
	);


	# Next step: create javadocs and upload them to the web
	do_step (
		name => "DAILY ONLINE JAVADOCS",
		log => "$dir/docs.txt",
		action => sub
		{
			#generate docs
			system ("ant docs > $dir/docs.txt") == 0 or 
				die ("docs failed with error code ", $? >> 8, "\n");
				
			#copy docs to website				
			my $retries = 5;
			my $fail_delay = 5;
			my $i = 0;
			while (1)
			{
				$i++;
				eval 
				{
					system ("scp -r $dir/apidoc/* pathvisio\@www.pathvisio.org:/home/pathvisio/apidoc 2>> $dir/docs.txt") == 0 or 
						die ("Could not copy javadocs, with error code " , $? >> 8, "\n");		
				};
				unless ($@) { last; } # break if success
				if ($i == $retries)
				{
					die ("Upload of javadoc failed after $i attempts\n");
				}
				print "scp failed, trying again...\n";
				sleep ($fail_delay);
			}

		}
	);

	# Next step: create a webstart and upload that to the web as well.
	do_step (
		name => "DAILY WEBSTART",
		log => "$dir/webstart.txt",
		action => sub
		{
			system ("ant prepare-webstart > $dir/webstart.txt") == 0 or 
				die ("prepare-webstart failed with error code ", $? >> 8, "\n");
			
			# substitute webstart codebase url
			
			system ("sed -i 's#codebase=\"http://www.pathvisio.org/webstart\"#codebase=\"http://www.pathvisio.org/webstart/daily\"#' $dir/webstart/www/*.jnlp") == 0 or
				die ("Couldn't substitute codebase url, with error code ", $? >> 8, "\n");
				
			# copy files to website
			
			my $retries = 5;
			my $fail_delay = 5;
			my $i = 0;
			while (1)
			{
				$i++;
				eval 
				{
					system ("scp -r $dir/webstart/www/* pathvisio\@www.pathvisio.org:/home/pathvisio/webstart/daily") == 0 or 				
						die ("Could not copy webstart, with error code " , $? >> 8, "\n");		
				};
				unless ($@) { last; } # break if success
				if ($i == $retries)
				{
					die ("Upload of webstart files failed after $i attempts\n");
				}
				print "scp failed, trying again...\n";
				sleep ($fail_delay);
			}

		}
	);

	# Next step: test gpmldiff shell scripts
	# if this fails, it means that gpmldiff.sh can't be run.
	do_step (
		name => "GPMLDIFF SHELL TEST",
		log => undef,
		action => sub
		{
			my $fnOut1 = "test.result1.txt";
			my $fnOut2 = "test.result2.txt";
			my $fnIn1 = "tools/gpmldiff/testcases/Simple1.1.gpml";
			my $fnIn2 = "tools/gpmldiff/testcases/Simple1.2.gpml";

			# test gpmldiff -o table option
			system ("sh gpmldiff.sh -o table $fnIn1 $fnIn2 > $fnOut1") == 0
				or die "gpmldiff -o table failed, $?";

			# test gpmldiff -o dgpml option
			system ("sh gpmldiff.sh -o dgpml $fnIn1 $fnIn2 > $fnOut2") == 0
				or die "gpmldiff -o dgpml failed, $?";
		}
	);

	# Next step: check that all source files contain a license header.
	# We tend to forget adding this.
	do_step (
		name => "LICENSE HEADER CHECK",
		log => "$dir/license_check.txt",
		action => sub
		{
			# check if all java files have the Apache License attached
			system (qq^find . -name "*.java" ! -name "Revision.java" | xargs -d '\n' grep -l "package org.pathvisio" | xargs -d '\n' grep -L "Apache License, Version 2.0" > $dir/license_check.txt^) == 0 or
				die ("find command failed with error code ", $? >> 8, "\n");
			
			# check that the number of lines in the output is equal to 0
			open INFILE, "$dir/license_check.txt" or die "License check output missing\n";
			my @files = <INFILE>;
			close INFILE;
			if (@files > 0) { die ("License header missing on some files\n"); }
		}
	);
	
	# test compilation of cytoscape-gpml
	do_step (
		name => "CYTOSCAPE-GPML",
		log => "$dir/cytoscape-gpml.txt",
		action => sub
		{
			my $cmd = 'ant -f tools/cytoscape-gpml/build.xml '.
				"-Dcytoscape.dir=$cytoscapedir ".
				'-Dwsdl.url=http://www.wikipathways.org/wpi/webservice/webservice.php?wsdl '.
				"> $dir/cytoscape-gpml.txt";
			print $cmd;
			system ($cmd) == 0 or 
				die ("cytoscape-gpml failed with error code ", $? >> 8, "\n");
		}
	);
	
	# Next step: check that java files have svn propset svn:eol-style native
	# Note: disabled because git-svn doesn't support propset atm.
	#~ do_step (
		#~ name => "SVN:EOL-STYLE PROPERTY",
		#~ log => "$dir/props.txt",
		#~ action => sub
		#~ {
			#~ our @javalist;
			#~ sub wanted { if (-f $_ && /\.java$/i && ! (/Revision.java$/)) { push @javalist, $File::Find::name; } }
			#~ find (\&wanted, "$dir/src");

			#~ system ("touch $dir/props.txt") == 0 or die ("Can't touch. Look ma, no hands? $!");
			#~ open OUTPUT, ">$dir/props.txt" or die $!;
			#~ my $cWrong = 0;
			
			#~ for my $file (@javalist)
			#~ {
				#~ if (`svn propget svn:eol-style $file` !~ /native/)
				#~ {
					#~ print OUTPUT $file, "\n";
					#~ $cWrong++;
				#~ }
			#~ }
			
			#~ close OUTPUT;
			
			#~ if ($cWrong > 0)
			#~ {
				#~ die "$cWrong java files are missing the svn:eolstyle property";
			#~ }
		#~ }
	#~ );

	# Next step: checkstyle
	do_step (
		name => "CHECKSTYLE",
		log => "$dir/cs_result.txt",
		action => sub
		{
			system ("ant checkstyle") == 0 or 
				die ("ant [checkstyle] failed with error code ", $? >> 8, "\n");
				
			#Now do a bit of magic so we only report NEW errors.
			my $cNew = 0;
			
			system ("touch $fnCheckstyle") == 0 or die ("Can't touch. Look ma, no hands? $!");
			open OLD, "$fnCheckstyle" or die $!;
			
			# create a hash of all old warnings
			# filter out the path before /src/ as it's different each run
			my %lOld =  map { $_ =~ s#^.*/src/#src/#; $_ => 1 } <OLD>;
			close OLD;
			
			open OUTPUT, ">$dir/cs_result.txt" or die $!;
			print OUTPUT "New warnings:\n";
			
			open NEW, "$dir/warnings.txt" or die $!;			
			while (my $line = <NEW>)
			{
				# filter out the path before /src/ as it's different each run
				$line =~ s#^.*/src/#src/#;
				if (!exists $lOld{$line})
				{
					$cNew++;
					print OUTPUT $line;
				}
			}
			close NEW;
			close OUTPUT;
			
			system ("mv $dir/warnings.txt $fnCheckstyle" ) == 0 or 
				die ("mv [checkstyle] failed with error code ", $? >> 8, "\n");
			
			# here is the logic bit: we bail out if there are any NEW warnings.
			if ($cNew > 0) { die "$cNew new checkstyle warnings" };
		}
	);

};

# send email in case of failures
# send a general error message, 
# plus the log of the step that failed, 
# plus the last 20 entries in the subversion log (so you can see who did it :) )

#if ($@)
{
	my $msg;
	my $subject;
	if ($@) 
	{
		$subject = "[BUILD] error during " . $steps[-1]{name};
		$msg = "Error during " . $steps[-1]{name} . ":\n" . $@;

		$msg .= "

An error occurred during the daily test procedure of PathVisio.  Below
you should find a log of the step that failed, plus the last 20 commit
messages. Please check if you checked in code that caused this error
and try to fix if at all possible.
";

	}
	else
	{
		$subject = "[BUILD] succes!";
		$msg = "Succes!";
	}
	
	# amend with svn log and revision
	my $svnversion = `svnversion`;
	die "svnversion failed: $?" if $?;
	system ("svn log --limit 20 > $dir/svnlog.txt");
	
	# read output from failed step
	my @lines;
	my $log = $steps[-1]{log};
	if (defined $log)
	{
		open IN, "$log" or die "Couldn't read from log file, $!";
		@lines = <IN>;
		close IN;
	}

	open IN, "$dir/svnlog.txt" or die "Couldn't read from log file, $!";
	my @svnlog = <IN>;
	close IN;

	#compose an email
	my $out;
	if ($fSendEmail)
	{
	    open $out, "| mail @emails -s '$subject'" or die "Failed to send email, $!";
	}
	else
	{
	    $out = \*STDOUT;
	}
	
	print $out "This is an autogenerated email, please do not reply.

SVN revision: $svnversion

The build system reported: $msg
";
	print $steps[-1]{error}, "\n\n";
	
	print $out "\n----------------\n\n";
	print $out @lines;	
	print $out "\n----------------\n\n";	
	print $out "Here is the latest subversion log:\n";
	print $out @svnlog;
	
	close $out;
}
