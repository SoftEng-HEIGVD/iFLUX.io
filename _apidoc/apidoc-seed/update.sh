#!/bin/bash

echo "Deleting previous version in ../../api/"
rm -fr ../../api/*

echo "Building new API site with grunt prod"
grunt prod --baseUrl=/api --apiUrl=https://iflux.herokuapp.com/

echo "Moving produced site in ../../api/"
mv build/* ../../api/
