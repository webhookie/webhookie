export VPC_ID=$(aws eks describe-cluster --name ${PRODUCT_NAME}-cluster --region $AWS_REGION  | jq -r '.cluster.resourcesVpcConfig.vpcId')
