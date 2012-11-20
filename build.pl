#!/usr/bin/perl

use warnings;
use strict;

use Archive::Zip qw{:ERROR_CODES :CONSTANTS};
use Cwd;
use POSIX qw{strftime};


my $now = strftime("%Y%m%d", localtime);
my $pwd = getcwd();
my $content_path = $pwd . '/content/30';

unless (-d "$pwd/build") {
  # create the build dir if not present
  mkdir("$pwd/build");
}

opendir(my $dh, $content_path) || die "can't opendir $content_path: $!\n";
my @dirs  = grep { /^[^.].*/ && -d "$content_path/$_" } readdir($dh);
closedir($dh);

foreach my $dir (@dirs) {
	
	my $input = "$content_path/$dir";
	my $output = "$pwd/build/$dir-$now.epub";
	
	if (-f $output)
	{
		unlink ($output);
	}
	
	chdir($input);
	
	my $zip = Archive::Zip->new();
	
	my $mimetype = $zip->addFile($input.'/mimetype', 'mimetype');
	$mimetype->desiredCompressionMethod('COMPRESSION_STORED');
	
	my $epub = $zip->addTree($input.'/EPUB/', 'EPUB');
	my $metainf = $zip->addTree($input.'/META-INF/', 'META-INF');
	
	unless ( $zip->writeToFileNamed($output) == AZ_OK ) {
		die 'Failed to write zip file for $output: $!\n';
	}
  
	chdir($pwd);
}
	
my $all = "epub-testsuite-$now.zip";
	
if (-f ("$all"))
{
	unlink ("$all");
}

my $build = $pwd . '/build/';

opendir(my $dh2, $build) || die "can't opendir $build: $!\n";
my @files  = grep { /^[^.].*/ && -f "$build/$_" } readdir($dh2);
closedir($dh2);

my $zip = Archive::Zip->new();

for my $file (@files)
{
	my $epubInc = $zip->addFile($build . $file, $file);
}

unless ( $zip->writeToFileNamed($all) == AZ_OK ) {
	die 'Failed to write zip for test suite: $!\n';
}
