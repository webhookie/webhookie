#!/bin/bash
# USAGE:
#    ./build.sh [option]
#
# DESCRIPTION
#    This is a helper script to manage the life cycle of the application
#
# OPTIONS
#    --requirements     Sets up all the requirements needed for the application lifecycle
#    --upload           Builds the docker image and push it to ecr
#    --deploy           Deploys the application and the ingress controller using helm
#    --install-app      Install the application using helm
#    --isntall-ic       Install the ingress controller using helm
#    --upgrade          Rebuilds the docker image, push it and upgrade the application using helm
#    --cleanup          Starts the deletion all all resources used to avoid future charges

function build_and_push() {
    echo -e "\e[32mUploading\e[0m"
    docker-compose -f deployment/docker/docker-compose.yml build
	docker tag ${PRODUCT_NAME}:${PRODUCT_VERSION} ${ECR_REPOSITORY}:${PRODUCT_VERSION}
	aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPOSITORY}:${PRODUCT_VERSION}
	docker push ${ECR_REPOSITORY}:${PRODUCT_VERSION}
}

function install_webhookie() {
  echo "Installing webhookie only"
  helm install webhookie -f ./branding/my-values.yaml \
    webhookie-repo/webhookie
}

function install_webhookie_all() {
  echo "Installing webhookie all"
  helm install webhookie-all webhookie-repo/webhookie-all
}

function install_webhookie_all_arm() {
  echo "Installing webhookie all"
  helm install webhookie-all -f ./arm-values.yaml webhookie-repo/webhookie-all
}

function install_webhookie_branded() {
  echo "Installing webhookie branded"
  helm install webhookie-branded -f ./branding/my-values.yaml \
    --set-file instanceTitle="./branding/detectify_assets/title.html" \
    --set-file instanceBody="./branding/detectify_assets/body.html" \
    --set-file instanceIcon="./branding/detectify_assets/favicon.ico" \
    --set-file instanceLogo="./branding/detectify_assets/logo.svg" \
    --set-file instanceHero="./branding/detectify_assets/hero.svg" \
    webhookie-repo/webhookie-branded
}

function install_webhookie_all_branded() {
  echo "Installing webhookie all branded"
  helm install webhookie-all-branded \
    --set-file instanceTitle="./branding/detectify_assets/title.html" \
    --set-file instanceBody="./branding/detectify_assets/body.html" \
    --set-file instanceIcon="./branding/detectify_assets/favicon.ico" \
    --set-file instanceLogo="./branding/detectify_assets/logo.svg" \
    --set-file instanceHero="./branding/detectify_assets/hero.svg" \
    webhookie-repo/webhookie-all-branded
}

function install_webhookie_all_branded_arm() {
  echo "Installing webhookie all branded form ARM"
  helm install webhookie-all-branded -f ./arm-values.yaml \
    --set WEBHOOKIE_MAIN_COLOR="'#090A3A'" \
    --set WEBHOOKIE_PAGE_TITLE="The API Hunt by webhookie" \
    --set-file instanceTitle="./branding/detectify_assets/title.html" \
    --set-file instanceBody="./branding/detectify_assets/body.html" \
    --set-file instanceIcon="./branding/detectify_assets/favicon.ico" \
    --set-file instanceLogo="./branding/detectify_assets/logo.svg" \
    --set-file instanceHero="./branding/detectify_assets/hero.svg" \
    webhookie-repo/webhookie-all-branded
}

function install_ic() {
    echo -e "\e[32mInstalling ingress controller\e[0m"
    helm install ${PRODUCT_NAME}ic \
    --set region=${AWS_REGION} \
    --set app=${PRODUCT_NAME} \
    --set vpcId=${VPC_ID} \
    ./deployment/helm/ic
}

function deploy () {
    install_ic
    sleep 30
    install_app
}

function upgrade() {
    build_and_push
    echo -e "\e[32mUpgrading\e[0m"
    helm upgrade ${PRODUCT_NAME} \
    --set region=${AWS_REGION} \
    --set app=${PRODUCT_NAME} \
    --set productCode=${PRODUCT_CODE} \
    --set vpcId=${VPC_ID} \
    --set mcp.imageName=${ECR_REPOSITORY} \
    --set mcp.imageVersion=${PRODUCT_VERSION} \
    ./deployment/helm/mcp
}

function clean_up() {
    echo -e "\e[32mCleaning up\e[0m"
	helm uninstall ${PRODUCT_NAME}
	sleep 30
	aws dynamodb delete-table --table-name ${PRODUCT_NAME}DimensionsTable
	aws ecr delete-repository --force --repository-name ${PRODUCT_NAME}
	eksctl delete cluster -f ./deployment/eksctl/${PRODUCT_NAME}cluster.yaml
}

install_requirements() {
    echo -e "\e[32mInstalling requirements\e[0m"
    echo -e "\e[32mInstalling AWS CLI Version 2\e[0m"
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
    unzip awscliv2.zip
    sudo ./aws/install

    echo -e "\e[32mInstalling kubectl\e[0m"
    curl -o kubectl "https://amazon-eks.s3.us-west-2.amazonaws.com/1.15.10/2020-02-22/bin/linux/amd64/kubectl"
    chmod +x ./kubectl
    mkdir -p $HOME/bin && cp ./kubectl $HOME/bin/kubectl && export PATH=$PATH:$HOME/bin
    echo 'export PATH=$PATH:$HOME/bin' >> ~/.bash_profile

    echo -e "\e[32mInstalling eksctl\e[0m"
    curl --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
    sudo mv /tmp/eksctl /usr/local/bin

    echo -e "\e[32mInstalling docker-compose\e[0m"
    sudo curl -L "https://github.com/docker/compose/releases/download/1.25.5/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

    echo -e "\e[32mInstalling helm\e[0m"
    curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
    chmod 700 get_helm.sh
    ./get_helm.sh

    echo -e "\e[32mInstalling jq\e[0m"
    sudo yum install jq -y

    echo -e "\n\n\e[43mConfirm the installs\e[0m"
    echo -e "\e[32mAWS CLI Version 2\e[0m"
    aws --version
    echo -e "\n\e[32mkubectl\e[0m"
    kubectl version --short --client
    echo -e "\n\e[32meksctl\e[0m"
    eksctl version
    echo -e "\n\e[32mdocker\e[0m"
    docker --version
    echo -e "\n\e[32mdocker-compose\e[0m"
    docker-compose --version
    echo -e "\n\e[32mhelm\e[0m"
    helm version --short
    echo -e "\n\e[32mjq\e[0m"
    jq --version
}

function help () {
    head -15 $0 | sed "/!\/bin\/bash/d" | sed -e "s/#//g"
}

case "$1" in
--requirements) install_requirements;;
--upload) build_and_push;;
--deploy) deploy;;
--install-app) install_app;;
--install-ic) install_ic;;
--upgrade) upgrade;;
--cleanup) clean_up;;
--webhookie) install_webhookie;;
--webhookie-all) install_webhookie_all;;
--webhookie-all-arm) install_webhookie_all_arm;;
--webhookie-branded) install_webhookie_branded;;
--webhookie-all-branded) install_webhookie_all_branded;;
--webhookie-all-branded-arm) install_webhookie_all_branded_arm;;
*) help;;
esac
