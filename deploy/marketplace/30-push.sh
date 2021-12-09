aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPOSITORY}:${PRODUCT_VERSION}
echo "RUN ./build.sh build and then ./build.sh aws"
