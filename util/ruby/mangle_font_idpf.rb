#!/usr/bin/ruby
#
# This script mangles a binary file using the IDPF obfuscation algorithm:
# http://www.idpf.org/epub/20/spec/FontManglingSpec.html
#
# Usage: mangle_font_idpf.rb FONT_FILE_NAME EPUB_UNIQUE_IDENTIFIER
#

require 'digest/sha1'
Digest::SHA1.hexdigest 'foo'

mangledFileName = ARGV[0]
uid = ARGV[1]

puts "uid: #{uid}"
puts(mangledFileName)

keyHex = Digest::SHA1.hexdigest(uid)

keyBytes = [keyHex].pack('H*')

puts "key:"
print keyBytes.unpack('H*')

puts "key class:"
puts keyBytes.class

mangledFile = open(mangledFileName, 'rb+')


fileIdx = 0
keyIdx = 0

while fileIdx < 1040 && ! mangledFile.eof do
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

