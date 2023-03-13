#!/bin/bash
 cd /
 cd c/repository
# Ova neredba sam verifikuje da li je dato CA tijelo izdalo proslijedjeni sertifikat, ali ne provjerava u crl listi da li je povucen
#openssl verify -CAfile rootca.pem $1

# Verify the certificate. Create a temporary RootCA by merging the CRL file with the RootCA certificate:
cat ca-cert.pem crl.pem > test.pem
# Next verify the user certificate against this newly created bundle containing rootCA + CRL file:
openssl verify -extended_crl -verbose -CAfile test.pem -crl_check $1


rez=`echo $?`
if [ "$rez" == "0" ]
		then
		echo "uspjesno"
		else
		touch error.txt
fi

rm test.pem
touch finish.txt
#read -p "Press any key to resume ..."