#!/usr/bin/env bash
set -euo pipefail

AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID:?Set AWS_ACCOUNT_ID}"
AWS_REGION="${AWS_REGION:-ap-south-1}"
ECR_PREFIX="${ECR_PREFIX:-order-management}"
IMAGE_TAG="${IMAGE_TAG:-latest}"

aws ecr get-login-password --region "${AWS_REGION}" | docker login --username AWS --password-stdin "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

for service in product-service inventory-service order-service; do
  repository="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_PREFIX}/${service}:${IMAGE_TAG}"
  docker build -f "services/${service}/Dockerfile" -t "${repository}" .
  docker push "${repository}"
done
