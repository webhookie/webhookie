echo "Creating $PRODUCT_NAME repository for product..."
export ECR_REPOSITORY=$(aws ecr-public create-repository \
    --repository-name $PRODUCT_NAME \
    --image-scanning-configuration scanOnPush=true \
    --region $AWS_REGION \
    | jq -r .repository.repositoryUri)
echo "repository created url: $ECR_REPOSITORY"
