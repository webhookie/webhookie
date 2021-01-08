function install_app() {
    echo -e "\e[32mInstalling app\e[0m"
    helm install ${PRODUCT_NAME} \
    --set region=${AWS_REGION} \
    --set app=${PRODUCT_NAME} \
    --set productCode=${PRODUCT_CODE} \
    --set vpcId=${VPC_ID} \
    --set wh.imageName=${ECR_REPOSITORY} \
    --set wh.imageVersion=${PRODUCT_VERSION} \
    --set DbUsername=wh-dev-user \
    --set DbPassword=5wOA2OYzb8NWN6TJ \
    --set DbHost=cluster0.dglao.mongodb.net \
    --set DbName=wh-dev-db \
    --set AmqpPassword=CGc2Q72jafhBj3dk0uycGDxAVfe8JAPt \
    --set AmqpVHost=nbcwvmaw \
    --set AmqpUsername=nbcwvmaw \
    --set AmqpHost=vulture.rmq.cloudamqp.com \
    --set WhIamIssuerUri=https://webhookie.au.auth0.com/ \
    --set WhIamJwkSetUri=https://webhookie.au.auth0.com/.well-known/jwks.json \
    --set WhIamJwsAlg=RS256 \
    --set WhSecurityAud=KbsOSWkS6GWvmh20mg24UdnrKwgUMfvK \
    --set WhSecurityLoginUri=http://localhost:8080 \
    --set WhSecurityRolesJsonPath=$$['https://webhookie.com/roles'] \
    ./helm/wh
}

function install_ic() {
    echo -e "\e[32mInstalling ingress controller\e[0m"
    helm install ${PRODUCT_NAME}ic \
    --set region=${AWS_REGION} \
    --set app=${PRODUCT_NAME} \
    --set vpcId=${VPC_ID} \
    ./helm/ic
}

function deploy () {
    install_ic
    sleep 30
    install_app
}

install_app
