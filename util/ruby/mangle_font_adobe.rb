#!/usr/bin/ruby
#
# This script mangles a binary file using the Adobe font obfuscation algorithm:
# http://www.adobe.com/content/dam/Adobe/en/devnet/digitalpublishing/pdfs/content_protection.pdf
#
# Usage: mangle_font_adobe.rb FONT_FILE_NAME EPUB_UNIQUE_IDENTIFIER_IN_HEX_FORMAT
#
# Note that the EPUB's unique identifier must be given as a plain HEX
# representation of the binary UUID data - no normalization of the format is
# performed (e.g. no whitespace and hyphen removal).
#

mangledFileName = ARGV[0]
uuid = ARGV[1]

puts "uuid: #{uuid}"
puts(mangledFileName)

keyBytes = [uuid].pack('H*')

puts "key:"
print keyBytes.unpack('H*')

puts "key class:"
puts keyBytes.class

mangledFile = open(mangledFileName, 'rb+')


fileIdx = 0
keyIdx = 0

while fileIdx < 1024 && ! mangledFile.eof do
	fileIdx += 1
	byte = mangledFile.readbyte
	keyByte = keyBytes.getbyte(keyIdx)
	puts "byte.class:"
	puts byte.class
	puts "keyByte.class:"
	puts keyByte.class

	mangledByte = byte ^ keyByte
	puts "mangledByte.class:"
	puts mangledByte.class

	mangledByteString = [mangledByte].pack('C*')

	puts "byte: #{byte}, keyIdx: #{keyIdx}, keyByte: #{keyByte}, mangledByte: #{mangledByte}, mangledByteString: #{mangledByteString}"
	mangledFile.pos = mangledFile.pos - 1
	mangledFile.write(mangledByteString)
	
	keyIdx = (keyIdx + 1) % keyBytes.size

end

mangledFile.close()

