FROM onb-docker.nexus-ci.corp.dev.vtb/rhel8/nginx-118:1-28
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY dist /dist
COPY infra-info /infra-info
RUN chmod +x /infra-info/version.sh
EXPOSE 8080
USER nginx
CMD ["nginx", "-g", "daemon off;"]
