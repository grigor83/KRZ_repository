#!/bin/bash
 cd /
 cd c/repository
 
 # $1 je putanja do userDir
 # $2 je userName
 # $3 je putanja do foldera u kojem je fajl za potpisivanje
 # $4 je fileName
  
# prvo treba potpisati fajl(koji se nalazi u proslijedjenom folderu) pomocu privatnog kljuca korisnika
openssl dgst -sha1 -sign $1/$2.key -out $3/$4.signature $3/$4

rez=`echo $?`
if [ "$rez" == "0" ]
		then
		echo "uspjesno"
fi

touch finish.txt
#read -p "Press any key to resume ..."