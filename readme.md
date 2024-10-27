# Installation
## Maven
### Add the JitPack repository to your build file
```
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```
### Add the dependency
```
	<dependency>
	    <groupId>com.github.deepslateorg</groupId>
	    <artifactId>abit</artifactId>
	    <version>Tag</version>
	</dependency>
```
# Spec
### key:
```
    [NNNNNNNN]   ╠ unsigned byte
    [BBBBBBBB]   ╗
       ....      ╠ number of bytes used for the key string is N+1 (UTF-8 encoded string)
    [BBBBBBBB]   ╝
```

### null:
```
    [0000|0000]
```

### boolean:
```
    true:  [0001|0001]
    false: [0000|0001]
```

### integer:
```
    [0XXX|0010] 
    [NNNNNNNN]   ╗
       ....      ╠ number of bytes used for the integer is X+1 (2s compliment & little-endian)    ║
    [NNNNNNNN]   ╝
```

### blob:
```
    [00XX|0011] 
    [NNNNNNNN]   ╗
       ....      ╠ number of bytes used for the integer is X+1 (2s compliment & little-endian)
    [NNNNNNNN]   ╝
    [BBBBBBBB]   ╗
       ....      ╠ number of bytes used for the blob is N
    [BBBBBBBB]   ╝
```

### string:
```
    [00XX|0100] 
    [NNNNNNNN]   ╗
       ....      ╠ number of bytes used for the integer is X+1 (2s compliment & little-endian)
    [NNNNNNNN]   ╝
    [SSSSSSSS]   ╗
       ....      ╠ number of bytes used for the string is N (UTF-8 encoded string)
    [SSSSSSSS]   ╝
```

### array:
```
    [00XX|0101] 
    [NNNNNNNN]   ╗
       ....      ╠ number of bytes used for the integer is X+1 (2s compliment & little-endian)
    [NNNNNNNN]   ╝
    [AAAAAAAA]   ╗
       ....      ╠ number of bytes used for the array is N (UTF-8 encoded string)
    [AAAAAAAA]   ╝
```

### tree:
```
    [00XX|0110] 
    [NNNNNNNN]   ╗
       ....      ╠ number of bytes used for the integer is X+1 (2s compliment & little-endian)
    [NNNNNNNN]   ╝
    [TTTTTTTT]   ╗
       ....      ╠ number of bytes used for the tree is N (UTF-8 encoded string)
    [TTTTTTTT]   ╝
```

### tree syntax:
```
    [  key   ] [ object ] ... [  key   ] [ object ]
```

### array syntax:
```
    [ object ] ... [ object ]
```

### other syntax:
* an integer must be the minimum amount of bytes required to represent it.
