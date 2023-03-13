#!/bin/bash
 cd /
 cd c/repository
  
 # $1 je folder sa fajlom u kojem je korisnikov privatni kljuc
 # $2 je fajl sa potpisom
 # $3 je originalni fajl na osnovu kojeg je nastao potpis
  
# prvo treba izdvojiti javni kljuc iz fajla sa privatnim kljucem
openssl rsa -in $1.key -out public.key -pubout

# a onda pomocu tog javnog kljuca, otiska i izvornog dokumenta verifikovati potpis
openssl dgst -sha1 -verify public.key -signature $2 $3

rez=`echo $?`
if [ "$rez" == "0" ]
		then
		echo "uspjesno"
		else
		touch error.txt
fi

rm public.key
touch finish.txt
#read -p "Press any key to resume ..."