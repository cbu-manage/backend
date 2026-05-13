FROM nginx:stable-alpine

# nginx 설정 복사
COPY default.conf.template /etc/nginx/conf.d/default.conf

# 정적 파일은 frontend 컨테이너에서 서빙하므로 제거
# COPY dist/ /usr/share/nginx/html

EXPOSE 80 443
