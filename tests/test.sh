#!/bin/bash
set -x

CONTAINER_ID="$(mktemp)"
DISTRO="centos7"
INIT="/usr/lib/systemd/systemd"

docker pull geerlingguy/docker-${DISTRO}-ansible:latest
docker run --detach --volume=${PWD}:/etc/ansible/roles/role_under_test:ro --privileged --volume=/sys/fs/cgroup:/sys/fs/cgroup:ro geerlingguy/docker-${DISTRO}-ansible:latest "${INIT}" > "${CONTAINER_ID}"
docker exec "$(cat ${CONTAINER_ID})" ansible-galaxy install -r /etc/ansible/roles/role_under_test/tests/requirements.yml
docker exec --tty "$(cat ${CONTAINER_ID})" env TERM=xterm ansible-playbook /etc/ansible/roles/role_under_test/tests/test.yml --syntax-check
docker exec --tty "$(cat ${CONTAINER_ID})" env TERM=xterm ansible-playbook /etc/ansible/roles/role_under_test/tests/test.yml

IDEMPOTENCE="$(mktemp)"

docker exec "$(cat ${CONTAINER_ID})" ansible-playbook /etc/ansible/roles/role_under_test/tests/test.yml | tee -a ${IDEMPOTENCE}

tail ${IDEMPOTENCE} | grep -q 'changed=0.*failed=0' && (echo 'Idempotence test: pass' && exit 0) || (echo 'Idempotence test: fail' && exit 1)
