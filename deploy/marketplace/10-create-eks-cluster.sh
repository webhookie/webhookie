envsubst < ./cluster.yaml >./${PRODUCT_NAME}-cluster.yaml
eksctl create cluster -f ./${PRODUCT_NAME}-cluster.yaml
