
* `GET /v2/catalog`: fixed catalog returned by OSB (static copy of catalog to be configured in mysql-release, single plan). 
* `PUT /v2/service_instances/:instance_id`:
  * save all query params to db
  * trigger async workflow
  * Response:
     * status: 202 accepted
     * body: 
        * dashboard URL: $external_host/manage/instances/$service_instance_id
        * operation: async workflow id ?
* `GET /v2/service_instances/:instance_id/last_operation`
   * return workflow status
* `PUT /v2/service_instances/:instance_id/service_bindings/:binding_id`
    - fetch the broker password from db.
    - reconstruct broker URL from service_instance_guid
    - then proxy bind request to the resulting broker URL and return values  
* `DELETE /v2/service_instances/:instance_id/service_bindings/:binding_id`
   * proxy unbind request to the broker URL
   * return received response
* `DELETE /v2/service_instances/:instance_id`
   * proxy delete request to the broker URL
   * bosh delete deployment
   * return merged status and description from actions
    
provision async workflow:
* generate manifest (sync)
   * deployment name = service_instance_id
   * service broker URL (external_host): p-mysql-service_instance_id.domain
   * generate random passwords (unless credhubs handles it)
* bosh deploy (async)
* proxy `PUT /v2/service_instances/:instance_id` and their params to service broker url (sync)



