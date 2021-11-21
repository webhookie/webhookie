install_app() {
  echo "Installing the app..."
  helm install "${PRODUCT_NAME}" \
    --set region="${AWS_REGION}" \
    --set app="${PRODUCT_NAME}" \
    --set productCode="${PRODUCT_CODE}" \
    --set vpcId="${VPC_ID}" \
    --set WH_IAM_ISSUER_URI=https://webhookie.au.auth0.com/ \
    --set WH_IAM_JWK_SET_URI=https://webhookie.au.auth0.com/.well-known/jwks.json \
    --set WH_IAM_JWS_ALG=RS256 \
    --set WH_SECURITY_AUD=http://localhost:8080 \
    --set WH_SECURITY_CLIENT_ID=nvKDmIK9Q5Zw1UKwpON8LE3tg9vZcXb4 \
    --set WH_SECURITY_ROLES_JSON_PATH="\$[\'https://webhookie.com/roles\']" \
    --set WH_SECURITY_GROUPS_JSON_PATH="\$[\'https://webhookie.com/groups\']" \
    --set WH_SECURITY_ENTITY_JSON_PATH="\$[\'https://webhookie.com/entity\']" \
    --set WH_SECURITY_AUTO_ASSIGN_CONSUMER_ROLE="true" \
    --set WH_SECURITY_OAUTH2_AUTHORIZATION_URI="authorize" \
    --set WH_SECURITY_OAUTH2_TOKEN_URI=oauth/token \
    --set WH_AMQP_PASSWORD=CGc2Q72jafhBj3dk0uycGDxAVfe8JAPt \
    --set WH_AMQP_V_HOST=nbcwvmaw \
    --set WH_AMQP_USERNAME=nbcwvmaw \
    --set WH_AMQP_HOST=vulture.rmq.cloudamqp.com \
    --set WH_CONSUMER_QUEUE=wh-customer.event \
    --set WH_CONSUMER_MISSING_HEADER_EXCHANGE=wh-customer \
    --set WH_MONGODB_URI="mongodb+srv://wh-user:8V3iEBda4EZe6Y3I@cluster0.47igq.mongodb.net/wh-dev-db?retryWrites=true&w=majority&maxPoolSize=200" \
    --set WEBHOOKIE_SECURITY_ALLOWED-ORIGINS=http://localhost:4300 \
    --set WEBHOOKIE_MAIN_COLOR="#090A3A" \
    --set WEBHOOKIE_PAGE_TITLE="The API Hunt by webhookie" \
    ./helm/webhookie
}

install_ic() {
  echo "\e[32mInstalling ingress controller\e[0m"
  helm install "${PRODUCT_NAME}".ic \
    --set region="${AWS_REGION}" \
    --set app="${PRODUCT_NAME}" \
    --set vpcId="${VPC_ID}" \
    ./helm/ic
}

deploy() {
  install_ic
  sleep 30
  install_app
}

install_all() {
  echo "Installing the app..."
  helm install "$PRODUCT_NAME" \
    --set WH_IAM_ISSUER_URI=http://localhost:8800/auth/realms/webhookie \
    --set WH_IAM_JWK_SET_URI=http://"$PRODUCT_NAME"-keycloak:8800/auth/realms/webhookie/protocol/openid-connect/certs \
    --set WH_IAM_JWS_ALG=RS256 \
    --set WH_SECURITY_AUD=webhookie_client \
    --set WH_SECURITY_CLIENT_ID=webhookie_client \
    --set WH_SECURITY_ROLES_JSON_PATH="\$.resource_access.webhookie_client.roles" \
    --set WH_SECURITY_GROUPS_JSON_PATH="\$.groups" \
    --set WH_SECURITY_ENTITY_JSON_PATH="\$.entity" \
    --set WH_SECURITY_AUTO_ASSIGN_CONSUMER_ROLE="true" \
    --set WH_SECURITY_OAUTH2_AUTHORIZATION_URI="/protocol/openid-connect/auth" \
    --set WH_SECURITY_OAUTH2_TOKEN_URI="/protocol/openid-connect/token" \
    --set WH_MONGODB_URI="mongodb://$PRODUCT_NAME-mongodb/webhookie-db?retryWrites=true&w=majority&maxPoolSize=200" \
    --set WEBHOOKIE_SECURITY_ALLOWED_ORIGINS=http://localhost:4200 \
    --set WEBHOOKIE_MAIN_COLOR="#090A3A" \
    --set WEBHOOKIE_PAGE_TITLE="The API Hunt by webhookie" \
    ./helm/webhookie-all
}

install_repo() {
  echo "Installing the app..."
  helm install webhookie-all \
   webhookie-repo/webhookie-all
}

install_repo
