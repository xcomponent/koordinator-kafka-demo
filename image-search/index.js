const koor = require("./koordinator.js");
const fs = require("fs");
const gIS = require("g-i-s");

const namespace = "Meetup";
const name = "ImageSearch";

koor.postCatalog({
  namespace: namespace,
  name: name,
  displayName: name,
  inputs: [
    {
      name: "terms",
      baseType: "string"
    },
    {
      name: "outputFile",
      baseType: "string"
    }
  ],
  schemaVersion: 0
});

koor.pollingLoop(namespace, name, async task => {
    const terms = task.inputData.terms;
    const outputFile = task.inputData.outputFile;

    console.log('search terms: ', terms);
    console.log('output file: ', outputFile);

    let outputData = '';

    gIS(terms, cb);

    async function cb(error, results) {
      if (error) {
        console.log(error);
        await koor.postStatus(task, { status: "Error", errorLevel: "Fatal" });
      }
      else {
          for(var i in results) {
            outputData += results[i].url + "\n";
          }
          
          fs.writeFileSync(outputFile, outputData);
          
          await koor.postStatus(task, { status: "Completed" });
      }
    }
});

