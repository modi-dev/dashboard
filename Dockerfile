ARG docker_proxy
FROM ${docker_proxy}/rhel8/nginx-118:1-28
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY --chown=1001:0 /app /app
RUN chmod -R 777 /app
EXPOSE 8080
USER nginx
CMD ["nginx", "-g", "daemon off;"]