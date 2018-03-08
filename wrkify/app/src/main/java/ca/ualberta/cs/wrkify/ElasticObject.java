/*
 * Copyright 2018 CMPUT301W18T18
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ca.ualberta.cs.wrkify;

import io.searchbox.core.DocumentResult;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.core.Search;

/**
 * ElasticObject provides a heritable class for
 * ElasticSearch aware implementations of Objects
 *
 * @see ElasticClient
 * @see ElasticTask
 * @see ElasticUser
 */

public class ElasticObject<T> {

    private ElasticClient client;
    private String elasticId;
    private Class<T> type;
    private T obj;

    /**
     * creates an ElasticObject from an elasticsearch id
     * and the ElasticClient Singleton
     * @param id the elasticsearch id
     */
    public ElasticObject(String id, Class<T> type) {
        this.client = ElasticClient.getInstance();
        this.elasticId = id;
        this.type = type;
    }

    /**
     * creates an ElasticObject from an elasticsearch id
     * and a provided ElasticClient
     * @param id the elasticsearch id
     * @param client the ElasticClient
     */
    public ElasticObject(String id, Class<T> type, ElasticClient client) {
        this.client = client;
        this.elasticId = id;
        this.type = type;
    }

    /**
     * creates an elasticObject from a new object to
     * be uploaded to eslasticsearch.
     * and the ElasticClient Singleton
     * @param obj the object to upload to elasticsearch
     */
    public ElasticObject(T obj, Class<T> type) throws Exception {
        this.client = ElasticClient.getInstance();
        this.obj = obj;
        this.type = type;

        //TODO support proper index
        Index index = new Index.Builder(obj).index("testing").type(this.type.getName()).build();
        DocumentResult result = (DocumentResult) this.client.execute(index);

        if (!result.isSucceeded()) {
            //TODO choose a better exception
            throw new Exception();
        }

        this.elasticId = result.getId();
    }

    /**
     * creates an elasticObject from a new object to
     * be uploaded to eslasticsearch.
     * and an ElasticClient
     * @param obj the object to upload to elasticsearch
     * @param client the ElasticClient
     */
    public ElasticObject(T obj, Class<T> type, ElasticClient client) throws Exception {
        this.client = client;
        this.obj = obj;
        this.type = type;

        //TODO support proper index
        Index index = new Index.Builder(obj).index("testing").type(this.type.getName()).build();
        DocumentResult result = (DocumentResult) this.client.execute(index);

        if (!result.isSucceeded()) {
            //TODO choose a better exception
            throw new Exception();
        }

        this.elasticId = result.getId();
    }

    public void update() throws Exception {
        //TODO support proper index
        Index index = new Index.Builder(this.obj)
                .index("testing").type(this.type.getName())
                .id(this.elasticId).build();
        DocumentResult result = (DocumentResult) this.client.execute(index);
    }

    public T getObj() {
        if (this.obj == null) {
            //TODO support proper index
            Get get = new Get.Builder("testing", this.elasticId).build();

            DocumentResult result = (DocumentResult) this.client.execute(get);

            this.obj = result.getSourceAsObject(this.type);
        }
        return this.obj;
    }

    public void refresh() {
        this.obj = null;
    }
}
