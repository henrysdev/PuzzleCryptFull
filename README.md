# PuzzleCrypt
<a href="http://www.youtube.com/watch?feature=player_embedded&v=hrrwGcQrlok"
 target="_blank"><img src="http://img.youtube.com/vi/hrrwGcQrlok/0.jpg" 
alt="PuzzleCrypt Demonstration" width="512" height="384" border="10" /></a>

An application for secure file fragmentation and reassembly. <br/>
Learn all about it here: http://henrysprojects.net/projects/file-frag-proto.html

### Usage  
  
#### Fragment a file:  
```
$ java PuzzleCrypt fragment <target-file> <num-fragments> <reassembly-password>
```
#### Reassemble a file:  
```
$ java PuzzleCrypt assemble <fragments-directory> <reassembly-password>
```
