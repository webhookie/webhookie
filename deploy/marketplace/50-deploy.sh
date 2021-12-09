function install_app() {
    echo -e "\e[32mInstalling app\e[0m"
    helm install ${PRODUCT_NAME} \
    --set region=${AWS_REGION} \
    --set vpcId=${VPC_ID} \
    ./app
}

function install_ic() {
    echo -e "\e[32mInstalling ingress controller\e[0m"
    helm install ${PRODUCT_NAME}ic \
    --set region=${AWS_REGION} \
    --set app=${PRODUCT_NAME} \
    --set vpcId=${VPC_ID} \
    ./ic
}

function deploy () {

# aws eks --region ${AWS_REGION} update-kubeconfig --name ${PRODUCT_NAME}-cluster
# kubectl apply -f ./app/docker-secret.yaml
# kubectl apply -f ./app/mongo-secret.yaml

     install_ic
#     sleep 30
#    install_app
}

deploy

function create_docker_secret_file() {
  kubectl create secret docker-registry --dry-run=client $secret_name \
    --docker-server=$DOCKER_REGISTRY_SERVER \
    --docker-username=$DOCKER_USER \
    --docker-password=$DOCKER_PASSWORD \
    --docker-email=$DOCKER_EMAIL -o yaml > docker-secret.yaml
}
