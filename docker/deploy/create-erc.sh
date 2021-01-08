export AWS_REGION=ap-southeast-2
export PRODUCT_NAME=wh-test
export PRODUCT_VERSION=v1.0.0
#replace the product code with the one you copied in Step 4.
export PRODUCT_CODE=dyu2pbedtxrubunz0g2a6u8q6
export ECR_REPOSITORY=$(aws ecr create-repository \
    --repository-name $PRODUCT_NAME \
    --image-scanning-configuration scanOnPush=true \
    --region $AWS_REGION \
    | jq -r .repository.repositoryUri)

