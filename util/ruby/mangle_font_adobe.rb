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

raise "\n\nUSAGE:\n  #{$0} file_to_[de]obfuscate plain_hex_publication_UUID\ne.g.:\n  #{$0} somefont.otf e6c3c1e1c8f44c0b8e083cb321b0c29e" unless ARGV[1]

mangledFileName = ARGV[0]
uuid = ARGV[1]

uuid = uuid.sub(/^urn:uuid:/i, "")
uuid = uuid.gsub(/[^0-9a-f]/i, "")

puts "Publication uuid: #{uuid}"

keyBytes = [uuid].pack('H*')

puts "XOR key:"
puts keyBytes.unpack('H*')

if (uuid !~ /[0-9a-f]{32}/i)
  puts "WARNING: supplied obfuscation key is not a 32-character hex code. (De)obfuscation results may be invalid."
end

puts "Obfuscating file [#{mangledFileName}]..."

mangledFile = open(mangledFileName, 'rb+')

fileIdx = 0
keyIdx = 0

while fileIdx < 1024 && ! mangledFile.eof do
	fileIdx += 1
	byte = mangledFile.readbyte
	keyByte = keyBytes.getbyte(keyIdx)

	mangledByte = byte ^ keyByte

	mangledByteString = [mangledByte].pack('C*')

	# puts "byte: #{byte}, keyIdx: #{keyIdx}, keyByte: #{keyByte}, mangledByte: #{mangledByte}, mangledByteString: #{mangledByteString}"
	mangledFile.pos = mangledFile.pos - 1
	mangledFile.write(mangledByteString)
	
	keyIdx = (keyIdx + 1) % keyBytes.size

end

mangledFile.close()

puts "Obfuscation finished."

