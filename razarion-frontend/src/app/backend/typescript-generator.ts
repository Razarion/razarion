import { HttpClient as HttpClientAdapter, RestResponse } from '../generated/razarion-share';
import { HttpClient } from '@angular/common/http';


export class TypescriptGenerator {
    static generateHttpClientAdapter(httpClient: HttpClient): HttpClientAdapter {
        return new class implements HttpClientAdapter {
            readonly httpClient = httpClient;
            request<R>(requestConfig: { method: string; url: string; queryParams?: any; data?: any; copyFn?: ((data: R) => R) | undefined; }): RestResponse<R> {
              return new Promise((resolve, error) => {
                    this.httpClient.request(requestConfig.method, "/" + requestConfig.url, { body: requestConfig.data }).subscribe({
                        next: (objectNameIds: any) => {
                            resolve(objectNameIds);
                        },
                        error: (err: any) => {
                            console.log(err);
                            error(err);
                        }
                    });
                });
            }
        }
    }
}
