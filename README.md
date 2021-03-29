# IMDB Search Project - Empathy Academy

## Starting ElasticSearch locally
We're going to work with ElasticSearch locally, running it as a Docker image. For that we'll need Docker CE and ElasticSearch (in our examples we'll be referring to Elastic's v.7.11.1).

First, download Docker Desktop. It can be found here: (https://www.docker.com/products/docker-desktop).

From the terminal, run the following commands:

`docker pull docker.elastic.co/elasticsearch/elasticsearch:7.11.1`

`docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.11.1`

To make sure it is running correctly:

`curl -XGET http://localhost:9200`

which should display, among others, the cluster name and uuid and its version. If everything's fine, then jump to the next section.

## Initializing the application: API deployment and running the app for the first time
First, clone the repository. Then, move inside the search_path folder, and use gradle to run the application. You can do everything from terminal using the following commands:

```
git clone https://github.com/Yori1999/search_path.git
cd search_path
./gradlew run
```

**¡IMPORTANT!**

For the moment, before running the application for the first time, you have to have downloaded the IMDB datasets you'll be working with. Continue reading the next section to learn about the datasets you're going to use and how to manage them.

## Indexing the IMDB dataset
After getting ElasticSearch up and running, you'll need to create an index and index some documents. We're going to work with some of the datasets that IMDB has made public, one for retrieving all data for IMDB media and another which has the ratings for (some of) that media.

You can find the original datasets here:

- [IMDB basic dataset](https://datasets.imdbws.com/title.basics.tsv.gz)
- [IMDB ratings dataset](https://datasets.imdbws.com/title.ratings.tsv.gz)

Once you download the files, rename them as "data.tsv" and "dataRatings.tsv" respectively and place them inside the "resources" folder of the project (/src/main/resources) so that the application can locate the files when running the indexing process.

As of now, the indexing process triggers whenever the application starts and no "imdb" index is found. If you want to reindex all the documents, simply delete the index you already have and re-run the application. This can be done via terminal with the following command:
`curl -XDELETE http://localhost:9200/imdb`.

You'll see how the indexing process goes; it'll tell you when it finishes, so that you can start using the Search API properly. In the meantime, you can check that the server is up and running accessing the following URL in any browser: http://localhost:8080/hello. Or, if you prefer it, running the following command: `curl -s http://localhost:8080/hello`.

### Additional Docker considerations
It's quite advisable to save the ElasticSearch's image after setting the index, so that you can have different versions of it.

We apologize in advance for the amount of time the indexing takes as of now; we're currently working on optimizing this operation.

## Using the Search API
The base URL for the Search API is the following: http://localhost:8080/search.

### Search parameters
All parameters we're going to describe in this section are optional. Basically, you can search by title, genre(s), type(s) of media and/or years.

## Additional documentation

### Micronaut 2.3.2 Documentation

- [User Guide](https://docs.micronaut.io/2.3.2/guide/index.html)
- [API Reference](https://docs.micronaut.io/2.3.2/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/2.3.2/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

### Feature elasticsearch documentation

- [Micronaut Elasticsearch Driver documentation](https://micronaut-projects.github.io/micronaut-elasticsearch/latest/guide/index.html)

### Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)

