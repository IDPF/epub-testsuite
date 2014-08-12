#!/usr/bin/ruby
#
# This script mangles a binary file using the IDPF obfuscation algorithm:
# http://www.idpf.org/epub/20/spec/FontManglingSpec.html
#
# Usage: mangle_font_idpf.rb FONT_FILE_NAME EPUB_UNIQUE_IDENTIFIER
#

raise "\n\nUSAGE:\n  #{$0} file_to_[de]obfuscate publication_identifier\ne.g.:\n"\
  "  #{$0} somefont.otf urn:uuid:e7c5d8f9-882d-4454-94e3-d755d179582d\nor:\n"\
  "  #{$0} somefont.otf code.google.com.epub-samples.wasteland-otf" unless ARGV[1]

require 'digest/sha1'
Digest::SHA1.hexdigest 'foo'

mangledFileName = ARGV[0]
uid = ARGV[1]

puts "uid: #{uid}"
puts(mangledFileName)

keyHex = Digest::SHA1.hexdigest(uid)

keyBytes = [keyHex].pack('H*')

puts "XOR key:"
puts keyBytes.unpack('H*')

puts "Obfuscating file [#{mangledFileName}]..."

mangledFile = open(mangledFileName, 'rb+')

fileIdx = 0
keyIdx = 0

while fileIdx < 1040 && ! mangledFile.eof do
	fileIdx += 1
	byte = mangledFile.readbyte
	keyByte = keyBytes.getbyte(keyIdx)

	mangledByte = byte ^ keyByte

	mangledByteString = [mangledByte].pack('C*')

	#puts "byte: #{byte}, keyIdx: #{keyIdx}, keyByte: #{keyByte}, mangledByte: #{mangledByte}, mangledByteString: #{mangledByteString}"
	mangledFile.pos = mangledFile.pos - 1
	mangledFile.write(mangledByteString)
	
	keyIdx = (keyIdx + 1) % keyBytes.size

end

mangledFile.close()

puts "Obfuscation finished."

