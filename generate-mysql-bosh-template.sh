#!/usr/bin/env bash

pushd path/to/cf-mysql-deployment
boshv2 interpolate ./cf-mysql-deployment.yml \
 -l bosh-lite/default-vars.yml \
 -o ./operations/add-broker.yml \
 -v cf_admin_username=cf-admin-username \
 -v cf_admin_password=cf-admin-password \
 -v cf_api_url=api.my-cf.com \
 -v app_domains="[e.g. myapps.my-cf.com]" \
 -v skip_ssl_validation=false \
-o ./operations/disable-cross-deployment-links.yml \
-v nats="{password: some-nats-password, user: nats, port: 4222, machines: [10.0.31.191]}"  \
 > cf-mysql.yml

FIXME:
need ops files for

properties:
  cf_mysql:
    broker:
      external_host: {{cf_mysql.broker.external_host}}
      auth_password: {{cf_mysql.broker.auth_password}}

and

director_uuid: {{bosh-director-uuid}}  #STACK LEVEL
name: '{{prefix}}{{guid}}' #SERVICE-INSTANCE LEVEL  e.g. <serviceid>