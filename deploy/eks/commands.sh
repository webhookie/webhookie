create-erc.sh

docker tag hookiesolutions/webhookie:1.0.0  735181786445.dkr.ecr.ap-southeast-2.amazonaws.com/wh-test:v1.0.0
aws ecr get-login-password --region "${AWS_REGION}" | docker login --username AWS --password-stdin "${ECR_REPOSITORY}":"${PRODUCT_VERSION}"

docker push "${ECR_REPOSITORY}":"${PRODUCT_VERSION}"

aws ecr describe-images --repository-name "${PRODUCT_NAME}"
aws ecr describe-images --repository-name "${PRODUCT_NAME}"

envsubst < ./cluster.yaml > ./"${PRODUCT_NAME}"-cluster.yaml

eksctl create cluster -f ./"${PRODUCT_NAME}"-cluster.yaml

aws eks describe-cluster --name "${PRODUCT_NAME}"-cluster --region "$AWS_REGION"

# shellcheck disable=SC2155
export VPC_ID=$(aws eks describe-cluster --name "${PRODUCT_NAME}"-cluster --region "$AWS_REGION" --output json  | jq -r '.cluster.resourcesVpcConfig.vpcId')


./deploy.sh # make sure you create ic as well as the app

kubectl get pods
aws elbv2 describe-load-balancers --query 'LoadBalancers[?VpcId==`'"$VPC_ID"'`].State' --output text
aws elbv2 describe-load-balancers --query 'LoadBalancers[?VpcId==`'"$VPC_ID"'`].DNSName' --output text

kubectl describe wh-test-c644c9bcd-m8wvc

aws elbv2 describe-load-balancers --query 'LoadBalancers[?VpcId==`'"$VPC_ID"'`].DNSName' --output text

helm status "${PRODUCT_NAME}"

aws elbv2 describe-load-balancers --query 'LoadBalancers[?VpcId==`'"$VPC_ID"'`].DNSName' --output text

kubectl logs -f wh-test-746597c985-qqbtl



helm uninstall "${PRODUCT_NAME}"
