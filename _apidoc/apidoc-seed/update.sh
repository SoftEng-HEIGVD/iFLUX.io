#!/bin/bash

echo "Deleting previous version in ../../api/"
rm -fr ../../api/*

echo "Building new API site with grunt prod"
grunt prod --baseUrl=/api --apiUrl=https://iflux.herokuapp.com/ --ifluxUrl=https://iflux.herokuapp.com/ --blogUrl=http://www.iflux.io

echo "Moving produced site in ../../api/"
mv build/* ../../api/
