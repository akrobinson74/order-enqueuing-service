class ManualEmbeddedMongoDbIntegrationTest {
//    private var mongodExecutable: MongodExecutable? = null
//    private var mongoTemplate: MongoTemplate? = null
//
//    @AfterEach
//    fun clean() {
//        mongodExecutable.stop()
//    }
//
//    @BeforeEach
//    @Throws(Exception::class)
//    fun setup() {
//        val ip = "localhost"
//        val port = 27017
//
//        val mongodConfig = MongodConfigBuilder().version(Version.Main.PRODUCTION)
//            .net(Net(ip, port, Network.localhostIsIPv6()))
//            .build()
//
//        val starter = MongodStarter.getDefaultInstance()
//        mongodExecutable = starter.prepare(mongodConfig)
//        mongodExecutable!!.start()
//        mongoTemplate = MongoTemplate(MongoClient(ip, port), "test")
//    }
//
//    @DisplayName(
//        "given object to save"
//            + " when save object using MongoDB template"
//            + " then object is saved"
//    )
//    @Test
//    @Throws(Exception::class)
//    fun test() {
//        // given
//        val objectToSave = BasicDBObjectBuilder.start()
//            .add("key", "value")
//            .get()
//
//        // when
//        mongoTemplate!!.save(objectToSave, "collection")
//
//        // then
//        assertThat(mongoTemplate!!.findAll(DBObject::class.java, "collection")).extracting("key")
//            .containsOnly("value")
//    }
}