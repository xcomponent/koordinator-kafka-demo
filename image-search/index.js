const fs = require("fs");
const gIS = require("g-i-s");

const terms = process.argv[2];
const outputFile = process.argv[3];

console.log('terms: ', terms);

let outputData = '';

gIS(terms, cb);

function cb(error, results) {
  if (error) {
    console.log(error);
  }
  else {
      for(var i in results) {
        outputData += results[i].url + "\n";
      }
      
      fs.writeFileSync(outputFile, outputData);
  }
}
