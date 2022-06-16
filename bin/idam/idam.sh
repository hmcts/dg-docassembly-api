#!/bin/sh
IDAM_USER_BASE_URL=http://localhost:4501
IDAM_S2S_BASE_URL=http://localhost:8489
DIR="$( cd "$( dirname "$0" )" && pwd )/"

CREATE_IDAM_USER="${DIR}idam-create-user.sh"
GET_IDAM_USER_TOKEN="${DIR}idam-get-user-token.sh"
GET_IDAM_S2S_TOKEN="${DIR}idam-get-s2s-token.sh"

${CREATE_IDAM_USER} test@TEST2.COM 123 ${IDAM_USER_BASE_URL}
echo "Authorization:"$(${GET_IDAM_USER_TOKEN} test@TEST2.COM 123 ${IDAM_USER_BASE_URL})
echo "ServiceAuthorization:"$(${GET_IDAM_S2S_TOKEN} sscs ${IDAM_S2S_BASE_URL})
