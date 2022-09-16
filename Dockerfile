FROM onb-docker.nexus-ci.corp.dev.vtb/rhel8/nginx-118:1-28
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY /app /app
RUN chown -R 1001 /app
EXPOSE 8080
USER nginx
CMD ["nginx", "-g", "daemon off;"]