package org.nephtys.genericvalueloader
import org.scalatest._
import upickle.default._

/**
  * Created by nephtys on 9/27/16.
  */
class LoaderSpec extends FlatSpec with Matchers {

  val correctfilename = "src/test/resources/example_correct.json"
  val wrongfilename = "nonexistingfile.json"
  val notJsonFile = "src/test/resources/example_nojson.json"
  val wrongJsonFile = "src/test/resources/example_wrongjson.json"

  val defautlstr = "N/A"
  val defaultflag = true
  val defaultIntValue = 42
  val defaultFloatingpoint : Double = 1.337

  case class ConfigExample(intValue : Int ,
                           flag : Boolean,
                           str : String,
                           floatingpoint : Double) {
    def similar(other : ConfigExample) : Boolean = {
      intValue == other.intValue && flag == other.flag && str.equals(other.str) && (Math.abs(floatingpoint - other.floatingpoint)
        < 0.0001)
    }
  }


  val serialFunc : ConfigExample => String = (t : ConfigExample)  =>  upickle.default.write(t)
  val deSerialFunc : String => ConfigExample = read[ConfigExample]
  val default : () => ConfigExample = () => ConfigExample(defaultIntValue, defaultflag, "This is a default value",
     defaultFloatingpoint)
  val normalConfig = ConfigExample(defaultIntValue, defaultflag, defautlstr,
    defaultFloatingpoint)
  val defaultOpt = Some(default)

  "Saved example json" should "be parsable" in {
    val source = scala.io.Source.fromFile(correctfilename)
    val lines = try source.mkString finally source.close()
    val fromfile = read[ConfigExample](lines)
    fromfile.intValue should be (42)
    fromfile.flag should be (false)
    fromfile.str should be ("This is a config")
  }

  "A Value Loader" should "load the value from file" in {

    val config = new GenericValueLoader[ConfigExample](() => correctfilename, None, serialize = serialFunc,
      deseralize = deSerialFunc
      , defaultFunc = defaultOpt)
    val value = config.getValue

    value.flag should be (false)
    value.str should be ("This is a config")
  }

  it should "throw a NoSuchElementException if default is impossible" in {
    val emptyvalue = new GenericValueLoader[ConfigExample](() => wrongfilename, None, serialize = serialFunc,
      deseralize = deSerialFunc
      , defaultFunc = None)

    a [NoSuchElementException] should be thrownBy {
      val v = emptyvalue.getValue
    }
  }

  it should "return default, if file not existant" in {
      val defaultval = default.apply()

    val config = new GenericValueLoader[ConfigExample](() => wrongfilename, None, serialize = serialFunc,
      deseralize = deSerialFunc
      , defaultFunc = defaultOpt)
    val value = config.getValue

    value should be (defaultval)
  }

  it should "return default, if file not json" in {
    val defaultval = default.apply()

    val config = new GenericValueLoader[ConfigExample](() => notJsonFile, None, serialize = serialFunc,
      deseralize = deSerialFunc
      , defaultFunc = defaultOpt)
    val value = config.getValue

    value should be (defaultval)

  }

  it should "return default, if file wrong json format" in {
    val defaultval = default.apply()

    val config = new GenericValueLoader[ConfigExample](() => wrongJsonFile, None, serialize = serialFunc,
      deseralize = deSerialFunc
      , defaultFunc = defaultOpt)
    val value = config.getValue

    value should be (defaultval)

  }



  val writtenFirst : ConfigExample = normalConfig.copy(str = "This is written first")
  val writtenSecond : ConfigExample = normalConfig.copy(str = "This is written second")


  it should "store changed values in file" in {
    val temporaryFile1 = "target/written_from_text1.json"
    GenericSetter.saveToFile(temporaryFile1, write(writtenFirst))

    val config = new GenericValueLoader[ConfigExample](() => temporaryFile1, None, serialize = serialFunc,
      deseralize = deSerialFunc
      , defaultFunc = None)


    //load value
    val value1 = config.getValue()

    val value2 = config.getValue()

    //set value
    config.setValue(writtenSecond)



    val value3 = config.getValue()
    val value4 = config.getValue()


    value1.similar(writtenFirst) should be (true)
    value2.similar(writtenFirst) should be (true)
    value3.similar(writtenSecond) should be (true)
    value4.similar(writtenSecond) should be (true)
    value4.similar(writtenFirst) should be (false)
  }

  it should "cache for a specific timeperiod and not longer" in {
    val temporaryFile2 = "target/written_from_text2.json"

    val timeout = 1000

    //write temp file
    GenericSetter.saveToFile(temporaryFile2, write(writtenFirst))

    val config = new GenericValueLoader[ConfigExample](() => temporaryFile2, Some(() => timeout), serialize = serialFunc,
      deseralize = deSerialFunc
      , defaultFunc = None)


    //load value
    val valueatstart = config.getValue

    //write new value
    GenericSetter.saveToFile(temporaryFile2, write(writtenSecond))


    //check after given time if still the same (should be equal)
    val valueDuringTimeout = config.getValue

    Thread.sleep(timeout + 100)

    //wait even longer and check again (should not be equal anymore)
    val valueAfterTimeout = config.getValue


    valueatstart.similar(valueDuringTimeout) should be (true)
    valueatstart.similar(valueAfterTimeout) should be (false)
  }

}
