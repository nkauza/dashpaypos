import { WebPlugin } from '@capacitor/core';

import type { DashpayModulePluginPlugin } from './definitions';

export class DashpayModulePluginWeb extends WebPlugin implements DashpayModulePluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async getSerial(options: { value: string }): Promise<{ value: string }> {
    console.log('getSerial', options);
    return options;
  }
  async print(options: { printString: string,EXTRA_ORIGINATING_URI:string,NewActivityLaunchOption:boolean, value: string }): Promise<{value: string}> {
    console.log('print string: ', options.printString);
    console.log('EXTRA_ORIGINATING_URI: ', options.EXTRA_ORIGINATING_URI);
    
    return options;
  }
  
  async pay(options: { REFERENCE_NUMBER: string,TRANSACTION_ID: string,OPERATOR_ID: string,ADDITIONAL_AMOUNT:string,AMOUNT: string,TRANSACTION_TYPE: string,EXTRA_ORIGINATING_URI:string, value: string}): Promise<{value: string}> {
    //console.logoptions('print string: ', options.printString);
    //console.log('EXTRA_ORIGINATING_URI: ', options.EXTRA_ORIGINATING_URI);
    //version: 1.6
	
    return options;
  }
}
