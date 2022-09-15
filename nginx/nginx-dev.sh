#!/bin/sh

sed -i -e "s#API_URL#${API_URL}#" /etc/nginx/nginx.conf
sed -i -e "s#API_HOST#${API_HOST}#" /etc/nginx/nginx.conf
sed -i -e "s#API_VERSION#${API_VERSION}#" /etc/nginx/nginx.conf