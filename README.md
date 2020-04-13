# kinesis-consumer
A generic Kinesis consumer.

### Usage

```
$ ./kinesis-consumer --help
kinesis-consumer 1.0.0
Usage: kinesis-consumer [options]

  -s, --stream <stream>  (Optional) The name of the Kinesis stream to consume events from.
  -r, --role <role>      (Optional) The IAM role to assume before consuming from Kinesis.
  -g, --region <region>  (Optional) The name of the region where the Kinesis stream is defined.
  -f, --file <file>      (Optional) A file to read streams configuration from.
  -j, --json <json>      (Optional) The streams configuration in JSON format.
For consuming a single stream, use --stream and optionally --role and --region.
For consuming one or more streams, use either --json or --file to provide all the configuration.
See the manual for the JSON format required.
  -h, --help             Prints this usage text.
```

The JSON schema for `--json` and `--file` is as follows:

```json
{
  "streams": [
    {
      "stream_name": "one"
    },
    {
      "stream_name": "two",
      "role_arn": "role-for-two"
    },
    {
      "stream_name": "three",
      "region": "us-west-1"
    },
    {
      "stream_name": "four",
      "region": "us-west-1",
      "role_arn": "role-for-four"
    }
  ]
}
```

In the example above:

  - stream `one` is in `us-east-1` and can be accessed with default credentials.
  - stream `two` is in `us-east-1` and can be accessed after assuming the IAM role `role-for-two`.
  - stream `three` is in `us-west-1` and can be accessed with default credentials.
  - stream `four` is in `us-west-1` and can be accessed after assuming the IAM role `role-for-four`.

### Credentials

The tool accesses AWS by using credentials set up in the same manner as the AWS CLI. For more information, see [here](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html).

### Build
`sbt assembly`
