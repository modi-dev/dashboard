ARG docker_proxy
FROM ${docker_proxy}/ubi8/nginx-120:1-106
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY --chown=1001:0 /dist /app
RUN chmod -R 777 /app
EXPOSE 8080
USER nginx
CMD ["nginx", "-g", "daemon off;"]