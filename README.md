# IMDB Search Project - Empathy Academy

## Installing gradle
For running the application correctly, you'll need to have gradle installed in your computer. To do so using SDKMAN, use the following command (notice we're using gradle v.6.4.1):

`sdk install gradle 6.4.1`

## Starting ElasticSearch locally
We're going to work with ElasticSearch locally, running it as a Docker image. For that we'll need Docker CE and ElasticSearch (in our examples we'll be referring to Elastic's v.7.11.1).

First, download Docker Desktop. It can be found here: (https://www.docker.com/products/docker-desktop).

From the terminal, run the following commands:

```sh
docker pull docker.elastic.co/elasticsearch/elasticsearch:7.11.1
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.11.1
```

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
You can check that the server is up and running accessing the following URL in any browser: http://localhost:8080/hello. Or, if you prefer it, running the following command: `curl -s http://localhost:8080/hello`. If you get "Hello World" as a response, then everything is OK.

Continue reading the next section to learn about the datasets you're going to use and how to manage them.

## Indexing the IMDB datasets
After getting ElasticSearch up and running, you'll need to create an index and index some documents. We're going to work with some of the datasets that IMDB has made public, one for retrieving all data for IMDB media and another which has the ratings for (some of) that media.

You can find the original datasets here:

- [IMDB basic dataset](https://datasets.imdbws.com/title.basics.tsv.gz)
- [IMDB ratings dataset](https://datasets.imdbws.com/title.ratings.tsv.gz)

Once you download the files, rename them as "data.tsv" and "dataRatings.tsv" respectively and place them inside the "resources" folder of the project (/src/main/resources) so that the application can locate the files when running the indexing process.

For triggering the indexing process, simply access http://localhost:8080/index, or run the command `curl http://localhost:8080/index`.

If you want to reindex all the documents, simply delete the index you already have and re-run the application. This can be done via terminal with the following command:
`curl -XDELETE http://localhost:9200/imdb`.

You'll see how the indexing process goes; it'll tell you when it finishes, so that you can start using the Search API properly. In the meantime, you can check that the server is up and running accessing the following URL in any browser: http://localhost:8080/hello. Or, if you prefer it, running the following command: `curl -s http://localhost:8080/hello`.

### Additional Docker considerations
It's quite advisable to save the ElasticSearch's image after setting the index, so that you can have different versions of it. To do so, use the following command:

`docker commit <id container> -q elastisearch:<name of the image>`

To obtain the "id container", run `docker ps`.

Alternatively, you can do both at the same time like this:

``docker commit `docker ps -q` elastisearch:<name of the image>``

And then, to restore that image:

`docker run --rm -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch_imdb:<name of the image>`

## Using the Search API
The base URL for the Search API is the following: http://localhost:8080/search. With this, all documents are returned, ordered with a certain criteria that takes into account average rating and number of votes to calculate the rating (popularity). Also, movies and TV Series have precedence, as it's what's most people comes searching for when using IMDB.

However, you can tune your search with several parameters that'll help you find what you're looking for.

### Search parameters
All parameters we're going to describe in this section are optional. Basically, you can search by title, genre(s), type(s) of media and/or years.

| Parameter | Description | Format | Basic Example |  "Complex" example |
| :--- | :--- | :--- | :--- | :--- |
| query | Title or original title (looks in both fields) of the media | \<string\> | Tron | Avengers Age of Ultron |
| genre | Comma separated list of genres. For an entry to match, at least one of its genres must be one of the specified ones | \<string\>[,\<string\>]* | Adventure | adventure,action,drama |
| type | Comma separated list of types. For an entry to match, its type must be one of the specified ones | \<string\>[,\<string\>]* | movie | movie,videogame |
| year | Comma separated list of year ranges. For an entry to match, it must have been released (startYear) during one of those periods of time | \<YYYY\>/\<YYYY\>[,\<YYYY\>/\<YYYY\>]* | 2000/2010 | 2000/2010,2012/2017 |

### What do I get?
Queries will return a JSON response with the following fields:
- `total`: the total number of hits. If it's greater than 10000, then returns 10000
- `items`: list of retrieved items/entries. If the number of matches is greater than 10, then it'll only return the first 10 results. Each of these results will have the following fields:
    - `id` (string): the unique identifier of the title. Matches its index identifier
    - `title` (string): the (primary) title
    - `genres` (array of strings): an array containing the different genres to which this title belongs
    - `type` (string): the type of media this title is classified as
    - `start_year` (string, YYYY format): the year in which this title was released or, in the case of TV Series, the year in which it began broadcasting
    - `end_year` (string, YYYY format) (optional): the year in which this title stopped being broadcasted/ended. Used only in the case of TV Series. If a result doesn't have end year, nothing is shown
    - `average_rating` (double): the average rating this title has
    - `num_votes` (integer): the number of votes this title has received in order to compute the average rating
- `aggregations`: list of different aggregations
    - `types`: total hits for each type of media present in the results
    - `genres`: total hits for each genre present in the results
    - `dates` (optional): total hits for each decade in case the year parameter was specified *PENDING CHANGES*

### Query examples

- Searches for all results: http://localhost:8080/search
- Searches for all documents whose title matches "Avengers": http://localhost:8080/search?query=Avengers
- Searches for all action or adventure movies or TV Series whose title contains "Avengers" (notice it doesn't matter how you write the text for your queries): http://localhost:8080/search?query=Avengers&genre=Action,adveNture&type=movie,TVSERIES
- Searches for all adventure movies and videogames released between 2000 and 2017: http://localhost:8080/search?&genre=adventure&type=movie,videoGame&year=2000/2017

**IMPORTANT:** If you try to search http://localhost:8080/search?query=, there will be no results returned. If you want all results, simply use the base query http://localhost:8080/search.

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

