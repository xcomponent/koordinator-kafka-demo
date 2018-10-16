const koor = require("./koordinator.js");
const fs = require("fs");
const gIS = require("g-i-s");

const namespace = "Meetup";
const name = "ImageSearch";

function doWork(terms, outputFile, cbWork) {
    console.log('search terms: ', terms);
    console.log('output file: ', outputFile);

    let outputData = '';

    gIS(terms, cb);

    async function cb(error, results) {
      if (error) {
        console.log(error);
        cbWork(error);
      }
      else {
          for(var i in results) {
            outputData += results[i].url + "\n";
          }
          
          fs.writeFileSync(outputFile, outputData);
          cbWork();
      }
    }

}

if (process.argv[2]) {
   const terms = process.argv[2];
   const outputFile = process.argv[3]; 

   doWork(terms, outputFile, () => {});
} else {
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

        doWork(terms, outputFile, async function cb(error) {
            if (error) {
                await koor.postStatus(task, { status: "Error", errorLevel: "Fatal" });
            } else {
              await koor.postStatus(task, { status: "Completed" });
            }
        })
    });
}
