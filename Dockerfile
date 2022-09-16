FROM onb-docker.nexus-ci.corp.dev.vtb/rhel8/nginx-118:1-28
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY --chown=1001:0 /app /app
COPY --chown=1001:0 /infra-info /infra-info
USER root
RUN chmod +rw /app/index.html
RUN chmod +rwx /infra-info/version.sh
USER 1001
EXPOSE 8080
USER nginx
CMD ["nginx", "-g", "daemon off;"]