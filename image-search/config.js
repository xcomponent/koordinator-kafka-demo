const token = process.env.WORKER_TOKEN;
const taskCatalogUrl =
  process.env.TASK_CATALOG_URL || "http://127.0.0.1:8099";
const pollingUrl = process.env.TASK_POLLING_URL || "http://127.0.0.1:7000";
const taskStatusUrl =
  process.env.TASK_STATUS_URL || "http://127.0.0.1:9999";

module.exports = {
  token,
  taskCatalogUrl,
  pollingUrl,
  taskStatusUrl
};
