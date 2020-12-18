client.test("Request executed successfully", function () {
    client.assert(response.status === 200, "Response status is not 200");
    client.assert(response.body.hasOwnProperty("access_token"), "Cannot find 'access_token' option in response");
    client.global.set("token", response.body.id_token);
    client.global.set("refresh_token", response.body.refresh_token);
});
