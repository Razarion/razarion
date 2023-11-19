import { HttpClient as HttpClientAdapter, RestResponse } from '../generated/razarion-share';
import { HttpClient } from '@angular/common/http';


export class TypescriptGenerator {
    static generateHttpClientAdapter(httpClient: HttpClient): HttpClientAdapter {
        return new class implements HttpClientAdapter {
            request<R>(requestConfig: { method: string; url: string; queryParams?: any; data?: any; copyFn?: ((data: R) => R) | undefined; }): RestResponse<R> {
                return <any>httpClient.request(requestConfig.method, requestConfig.url, { body: requestConfig.data }).toPromise();
            }
        }
    }
}