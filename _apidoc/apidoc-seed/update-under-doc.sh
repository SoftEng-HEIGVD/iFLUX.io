#!/bin/bash

echo "Deleting previous version in ../../api/"
rm -fr ../../api/*

echo "Building new API site with grunt prod"
grunt prod --baseUrl=/doc/api --apiUrl=localhost:3000 --ifluxUrl=localhost:3000 --blogUrl=http://localhost/doc

echo "Moving produced site in ../../api/"
mv build/* ../../api/
