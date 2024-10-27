╔══ key length ═══╦═ key String ════...═════════════════╦═ 
║ x x x x x x x x ║ x x x x x x x x ... x x x x x x x x ║

key length[1] // unsigned byte for the length of the key
key [key length + 1] // UTF-8 encoded string as the key
object type[1]  00 null     0 0 0 0 | 0 0 0 0
                01 boolean  0 0 0 x | 0 0 0 1   // x is boolean value
                02 integer  0 x x x | 0 0 1 0   // x is which type of integer
                03 blob     0 0 x x | 0 0 1 1   // x + 1 is how many bytes are used to describe the length of the object
                04 string   0 0 x x | 0 1 0 0   // x + 1 is how many bytes are used to describe the length of the object
                05 array    0 0 x x | 0 1 0 1   // x + 1 is how many bytes are used to describe the length of the object
                06 tree     0 0 x x | 0 1 1 0   // x + 1 is how many bytes are used to describe the length of the object

if (type & 0x0f) == 00:
    // no object

if (type & 0x0f) == 01:
    boolean[0] is (type & 0xf0)

if (type & 0x0f) == 02:
    if type & 0x80:
        -int[(type & 0x70)+1]
    else:
        int[(type & 0x70)+1]

if 03 <= (type & 0x0f) <= 05:
    object length[x+1] (little-endian byte order)
    object[object length]

inside object (00 - 07):
data[(obj & 3) + 1]

inside object (08 - 12):
size[(type&0xf0)>>4]
data[size]

no object (13 - 15)

if not of type 08 - 12, the 4 biggest bits in the type MUST be 0


{   
    // key length & key encoded as UTF-8
    [ 00000011 ] [ 01101110 | 01110101 | 01101100 ]
    // null
    [ 00000000 ]

    // key length & key encoded as UTF-8
    [ 00000011 ] [ 01100010 | 01101111 | 01101100 ]
    // boolean, X stands for true or false (1 / 0)
    [ 000X0001 ]

    // key length & key encoded as UTF-8
    [ 00000011 ] [ 01101001 | 01101110 | 01110100 ]
    // integer (small-endian), XXX + 1 stands for number of bytes used to describe a 2s compliment integer (must always use minimum number of bytes for the given number to be valid)
    [ 0XXX0010 ] [ ........ ]

    // integer examples
    [ 00000010 ] [ 01000101 ] // valid
    [ 00010010 ] [ 01000101 ] // invalid, underextended
    [ 00000010 ] [ 01000101 01000101 ] // invalid, overextended
    [ 00010010 ] [ 01000101 00000000 ] // invalid, value would fit inside a single byte

    // key length & key encoded as UTF-8
    [ 00000011 ] [ 01100010 | 01111001 | 01110100 ]
    // blob, XX stands for number of bytes to describe length of blob, blob is essentially an integer
    [ 00XX0011 ] [ ........ ] [ ........ ]

    // blob examples
    [ 00010011 ] [ ]
}