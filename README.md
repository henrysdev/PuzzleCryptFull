
                                                                                                                             
88888888ba                                      88                ,ad8888ba,                                                 
88      "8b                                     88               d8"'    `"8b                                         ,d     
88      ,8P                                     88              d8'                                                   88     
88aaaaaa8P'  88       88  888888888  888888888  88   ,adPPYba,  88             8b,dPPYba,  8b       d8  8b,dPPYba,  MM88MMM  
88""""""'    88       88       a8P"       a8P"  88  a8P_____88  88             88P'   "Y8  `8b     d8'  88P'    "8a   88     
88           88       88    ,d8P'      ,d8P'    88  8PP"""""""  Y8,            88           `8b   d8'   88       d8   88     
88           "8a,   ,a88  ,d8"       ,d8"       88  "8b,   ,aa   Y8a.    .a8P  88            `8b,d8'    88b,   ,a8"   88,    
88            `"YbbdP'Y8  888888888  888888888  88   `"Ybbd8"'    `"Y8888Y"'   88              Y88'     88`YbbdP"'    "Y888  
                                                                                               d8'      88                   
                                                                                              d8'       88                   


An application for secure file fragmentation and reassembly. 
Learn all about it here: http://henrysprojects.net/projects/file-frag-proto.html
[![PuzzleCrypt Demonstration](https://img.youtube.com/vi/hrrwGcQrlok&t=1s/0.jpg)](https://www.youtube.com/watch?v=hrrwGcQrlok&t=1s)

### Usage
Fragment a file:
$ java PuzzleCrypt fragment <target-file> <num-fragments> <reassembly-password>

Reassemble a file:
$ java PuzzleCrypt assemble <fragments-directory> <reassembly-password>