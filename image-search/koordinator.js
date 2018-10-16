const request = require("request-promise-native");
const config = require("./config.js");

const token = config.token;

async function postCatalog(catalogTask) {
  console.log("Posting catalog task", catalogTask);

  try {
    return await request({
      method: "POST",
      uri: `${config.taskCatalogUrl}/api/TaskCatalog`,
      json: true,
      headers: {
        Authorization: "Bearer " + token
      },
      simple: false,
      resolveWithFullResponse: true,
      body: { catalogTaskDefinitions: [catalogTask] }
    });
  } catch (e) {
    console.error(e);
    return undefined;
  }
}

async function pollTask(namespace, name, cb) {
  try {
    const result = await request({
      method: "GET",
      uri:
        `${config.pollingUrl}/api/Poll?catalogTaskDefinitionNamespace=` +
        namespace +
        "&catalogTaskDefinitionName=" +
        name,
      simple: false,
      resolveWithFullResponse: true,
      headers: {
        Authorization: "Bearer " + token
      },
      json: true
    });

    if (result.statusCode === 200) {
      console.log(result.body);
      return result.body;
    }
    return undefined;
  } catch (err) {
    console.error(err);
    return undefined;
  }
}

async function postStatus(task, status) {
  const postedStatus = {
    ...status,
    taskInstanceId: task.id
  };

  console.log("Posting status", postedStatus);

  try {
    const result = await request({
      method: "POST",
      uri: `${config.taskStatusUrl}/api/TaskStatus`,
      headers: {
        Authorization: "Bearer " + token
      },
      simple: false,
      resolveWithFullResponse: true,
      json: true,
      body: postedStatus
    });

    console.log(result.statusCode, result.body);
  } catch (e) {
    console.error(e);
  }
}

async function pollingLoop(namespace, name, cb) {
  const interval = 5000;
  const pollingFunction = async () => {
    const task = await pollTask(namespace, name);
    if (task) {
      await cb(task);
    }
  };
  setInterval(pollingFunction, interval);
}

module.exports = {
  postCatalog,
  postStatus,
  pollTask,
  pollingLoop
};
