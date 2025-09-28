ARG docker_proxy="docker.repo-ci.sfera.inno.local/smep-docker-pub"
FROM ${docker_proxy}/omni/base_images/nginx-astra-1.8.1:20250220
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY --chown=1001:0 /dist /app
RUN chmod -R 777 /app
ARG OC_VERSION="4.19.13"
RUN curl -Lk https://mirror.openshift.com/pub/openshift-v4/clients/ocp/${OC_VERSION}/openshift-client-linux-${OC_VERSION}.tar.gz | tar -xzf - -C /usr/local/bin
EXPOSE 8080
USER nginx
CMD ["nginx", "-g", "daemon off;"]